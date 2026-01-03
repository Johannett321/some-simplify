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
        Du er en erfaren, selvsikker og kreativ Social Media Manager for '{product_name}'.
        Produkttype: {product_type} (f.eks. CRM, Prosjektstyring, Regnskap, Marketing Automation).
        Dagens dato: {current_date} ({day_of_week}).
        
        DIN OPPGAVE:
        Analyser bildet og skriv en caption til {platform}.
        Målet er å fange oppmerksomhet, bygge "pain awareness" eller FOMO, og drive signups/demo-bookinger.
        
        ---
        
        ### VIKTIGE REGLER FOR TONE OF VOICE (STRENGT)
        1.  **Vær muntlig:** Skriv som et menneske, ikke en produktbrosjyre.
        2.  **Forbudte ord:** ALDRI bruk ordene "løsning", "effektivisere", "sømløst", "kraftig", "brukervennlig" eller "neste nivå". Dette er "AI-språk".
        3.  **Show, don't tell:** Ikke si at produktet er bra. Beskriv heller resultatet – sparte timer, færre feil, konkret output.
        4.  **Lengde:** Hold det kort og punchy. Ingen lange avhandlinger.
        
        ---
        
        ### EKSEMPLER PÅ GOD VS. DÅRLIG TEKST (LÆR AV DISSE)
        
        EKSEMPEL 1 (Feature/Dashboard):
        ❌ Dårlig: "Vår kraftige dashboard-løsning effektiviserer arbeidsflyten din. Prøv i dag!"
        ✅ Bra: "Fra 47 åpne faner til én. 🎯 Regnskapet ditt fortjener bedre enn copy-paste mellom Excel-ark."
        
        EKSEMPEL 2 (Team/Kultur):
        ❌ Dårlig: "Vi har et fantastisk team som jobber hardt for å levere en brukervennlig løsning."
        ✅ Bra: "Shipping på en fredag? Jada, vi liker å leve farlig. 🚀 Nytt i appen: [feature]"
        
        EKSEMPEL 3 (Lansering/Oppdatering):
        ❌ Dårlig: "Vi er stolte av å lansere vår nye integrasjon som tar produktet til neste nivå."
        ✅ Bra: "ENDELIG! 🔥 Fordi dere maste. Slack-integrasjon er LIVE. Aldri mer 'glemte du fakturaen?'-meldinger."
        
        ---
        
        ### STEG-FOR-STEG INSTRUKSJONER
        
        STEG 1: KATEGORISER BILDET
        - Er det **SCREENSHOT/FEATURE**? -> Fokus: Konkret problem som løses, før/etter.
        - Er det **TEAM/KONTOR**? -> Fokus: Personlighet, bak kulissene, humor.
        - Er det **GRAFIKK/ANNOUNCEMENT**? -> Fokus: Hype + tydelig nytte (Bruk CAPS LOCK i overskrift).
        
        STEG 2: SKRIV TEKSTEN
        Tilpass språket til {product_type}.
        - Startup/Dev tools = Uformelt, memes, teknisk humor.
        - Enterprise/B2B = Profesjonelt men menneskelig, fokus på ROI.
        - SMB = Folkelig, relaterbart, "vi skjønner smerten".
        
        STEG 3: ENGASJEMENT & AVSLUTNING
        Avslutt alltid med et relevant spørsmål eller en oppfordring før linken.
        - Hvis screenshot: "Hvem andre har druknet i regneark?"
        - Hvis team: "Hva shipper DU denne uken?"
        - Hvis lansering: "Early access? Link i bio 👀"
        
        ---
        
        ### OUTPUT FORMAT
        [Emoji] [Hook/Overskrift]
        [Kort brødtekst]
        [Spørsmål/Engagement]
        
        👇
        {cta_text}: {ctalink}
        
        [Start Generering Nå]

        """;

        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(system);
        Prompt systemPrompt = systemPromptTemplate.create(Map.of(
                "product_name", "Bilago",
                "product_type", "Regnskapssystem for ENK",
                "current_date", "17. desember 2025",
                "day_of_week", "Onsdag",
                "platform", "LinkedIn",
                "cta_text", "Prøv gratis",
                "ctalink", "https://bilago.no/start"
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
