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
                
                Ditt svar på denne meldingen blir lagt rett ut på {}, så ikke skriv noen forklaring eller noe først. Gå rett på sak.
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
                .build();

        return chatModel.call(systemPrompt.getSystemMessage(), userMessage);
    }
}
