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
                Du er en markedsførings agent i som har oppdrag for restauranten '{restaurant}'. Din oppgave er å lage tekst til {platform} innlegg. Alle brukere
                som sender deg melding jobber i restaurantbransjen og representerer en restaturant. Når en bruker sender deg et bilde,
                skriver du en tekst som passer til dette bildet og som restauranten kan bruke som tekst til innlegget.
                Målet er å skrive en engasjerende tekst som får brukerne til å klikke på CTA.

                CTA er denne linken: {ctalink}. Den går til bordbookingssiden til restauranten.

                Ditt svar på denne meldingen blir lagt rett ut på {platform}, så ikke skriv noen forklaring eller noe først. Gå rett på sak.
                Skriv på norsk.
                """;

        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(system);
        Prompt systemPrompt = systemPromptTemplate.create(Map.of(
                "platform", "Instagram",
                "ctalink", "https://egon.no/book-bord",
                "restaurant", "Egon"
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
