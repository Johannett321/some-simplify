package com.somesimplify.somesimplify.service;

import com.somesimplify.model.PlatformType;
import com.somesimplify.model.PostStatus;
import com.somesimplify.somesimplify.model.ContentFile;
import com.somesimplify.somesimplify.model.Post;
import com.somesimplify.somesimplify.repository.ContentFileRepository;
import com.somesimplify.somesimplify.repository.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.content.Media;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.net.URI;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final ChatModel chatModel;
    private final S3Service s3Service;
    private final PostRepository postRepository;
    private final ContentFileRepository contentFileRepository;

    @Transactional
    public void generatePosts(String tenantId) {
        List<ContentFile> contentFiles = contentFileRepository.findAllByTenantId(tenantId);
        contentFiles.forEach(this::generateSinglePost);
    }

    public void generateSinglePost(ContentFile contentFile) {
        log.info("Lager instagram post for bilde '{}'", contentFile.getFileName());
        // choose a random post (the newer the image, the more likely it's picked)
        String text = generateTextForPost(contentFile);
        log.info(contentFile.getS3Key() + " " + text);

        Post post = new Post();
        post.setText(text);
        post.setContentFiles(List.of(contentFile));
        post.setPlatforms(List.of(PlatformType.INSTAGRAM));
        post.setStatus(PostStatus.DRAFT);
        postRepository.save(post);
    }

    public String generateTextForPost(ContentFile contentFile) {
        String system = """
                Du er en erfaren, selvsikker og kreativ Social Media Manager for '{place_name}'.
                Stedskonsept: {place_type} (f.eks. Nattklubb, Brun Pub, Fine Dining, Bakeri).
                Dagens dato: {current_date} ({day_of_week}).
                
                DIN OPPGAVE:
                Analyser bildet og skriv en caption til {platform}.
                Målet er å fange oppmerksomhet, bygge "craving" eller stemning, og drive trafikk.
                
                ---
                
                ### VIKTIGE REGLER FOR TONE OF VOICE (STRENGT)
                1.  **Vær muntlig:** Skriv som et menneske, ikke en reklameplakat.
                2.  **Forbudte ord:** ALDRI bruk ordene "velkommen", "vi tilbyr", "kom til oss", "deilig", "smakfull" eller "unik". Dette er "AI-språk".
                3.  **Show, don't tell:** Ikke si at maten er god. Beskriv heller at osten smelter eller at glasset er kaldt.
                4.  **Lengde:** Hold det kort og punchy. Ingen lange avhandlinger.
                
                ---
                
                ### EKSEMPLER PÅ GOD VS. DÅRLIG TEKST (LÆR AV DISSE)
                
                EKSEMPEL 1 (Mat/Burger):
                ❌ Dårlig: "Kom og smak vår deilige burger som vi tilbyr i dag. Velkommen!"
                ✅ Bra: "Sjekk den skorpen... 🤤 Trenger du en unnskyldning for å spise burger på en tirsdag? Her er den."
                
                EKSEMPEL 2 (Nattklubb/Fest):
                ❌ Dårlig: "Vi har god stemning på dansegulvet. Kom og dans med oss."
                ✅ Bra: "Fullt hus og kaos på den beste måten! 💥 Hvem stenger stedet med oss i natt?"
                
                EKSEMPEL 3 (Plakat/Tilbud):
                ❌ Dårlig: "Her er plakaten for fredag. Vi har tilbud på biff til 159 kroner."
                ✅ Bra: "FREDAGSBIFF! 🥩 Kun 159,- hele kvelden. Starter helgen nå, eller?"
                
                ---
                
                ### STEG-FOR-STEG INSTRUKSJONER
                
                STEG 1: KATEGORISER BILDET
                - Er det **MAT/DRIKKE**? -> Fokus: Sanselighet (smak, lukt, syn).
                - Er det **FOLK/INTERIØR**? -> Fokus: Stemning, sosialt, "vibe".
                - Er det **PLAKAT/TEKST**? -> Fokus: Informasjon + Hype (Bruk CAPS LOCK i overskrift).
                
                STEG 2: SKRIV TEKSTEN
                Tilpass språket til {place_type}.
                - Nattklubb = Korte setninger, emojis, hype.
                - Fine Dining = Roligere, mer elegant, men fortsatt ikke stivt.
                - Pub/Bar = Folkelig, humor, jovialt.
                
                STEG 3: ENGASJEMENT & AVSLUTNING
                Avslutt alltid med et relevant spørsmål eller en oppfordring før linken.
                - Hvis bildet er sosialt: "Tag en venn..."
                - Hvis bildet er mat: "Sulten enda?"
                - Hvis plakat: "Sikre deg bord før det er fullt."
                
                ---
                
                ### OUTPUT FORMAT
                [Emoji] [Hook/Overskrift]
                [Kort brødtekst]
                [Spørsmål/Engagement]
                
                👇
                Book her: {ctalink}
                
                [Start Generering Nå]

                """;

        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(system);
        Prompt systemPrompt = systemPromptTemplate.create(Map.of(
                "place_name", "Egon",
                "place_type", "Restaurant",
                "current_date", "17. desember 2025",
                "day_of_week", "Onsdag",
                "platform", "Instagram",
                "ctalink", "https://egon.no/book-bord"
        ));

        UserMessage userMessage = UserMessage.builder()
                .media(new Media(MimeTypeUtils.IMAGE_PNG,
                        URI.create(s3Service.generatePresignedUrl(contentFile.getS3Key()))))
                .text("Kan du skrive en tekst for dette bildet vi planlegger å legge ut på Instagram?")
                .build();

        return chatModel.call(systemPrompt.getSystemMessage(), userMessage);
    }

    public List<Post> getPosts(LocalDate fromDate, LocalDate toDate, PostStatus status) {
        List<Post> posts = postRepository.findAll();

        return posts.stream()
                .filter(post -> {
                    if (status != null && post.getStatus() != status) {
                        return false;
                    }

                    if (post.getPublishAt() == null) {
                        return status == PostStatus.DRAFT;
                    }

                    LocalDate postDate = post.getPublishAt().toLocalDate();

                    if (fromDate != null && postDate.isBefore(fromDate)) {
                        return false;
                    }

                    if (toDate != null && postDate.isAfter(toDate)) {
                        return false;
                    }

                    return true;
                })
                .toList();
    }

    public Post getPostById(String id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
    }

    @Transactional
    public Post updatePost(String id, String text, OffsetDateTime publishAt, PostStatus status) {
        Post post = getPostById(id);

        if (text != null) {
            post.setText(text);
        }

        if (publishAt != null) {
            post.setPublishAt(publishAt);
        }

        if (status != null) {
            post.setStatus(status);
        }

        return postRepository.save(post);
    }

    public OffsetDateTime getSuggestedPublishDate() {
        List<Post> scheduledPosts = postRepository.findAll().stream()
                .filter(post -> post.getPublishAt() != null)
                .filter(post -> post.getStatus() == PostStatus.SCHEDULED || post.getStatus() == PostStatus.PUBLISHED)
                .sorted(Comparator.comparing(Post::getPublishAt).reversed())
                .toList();

        if (scheduledPosts.isEmpty()) {
            // If no posts are scheduled, suggest tomorrow
            return OffsetDateTime.now(ZoneOffset.UTC).plusDays(1).withHour(12).withMinute(0).withSecond(0).withNano(0);
        }

        OffsetDateTime lastScheduledDate = scheduledPosts.get(0).getPublishAt();
        OffsetDateTime suggestedDate = lastScheduledDate.plusDays(3);

        // Make sure the suggested date is not in the past
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        if (suggestedDate.isBefore(now)) {
            suggestedDate = now.plusDays(1);
        }

        // Set time to noon
        return suggestedDate.withHour(12).withMinute(0).withSecond(0).withNano(0);
    }
}
