package com.somesimplify.somesimplify.service;

import com.google.genai.Client;
import com.google.genai.types.Blob;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageGenerationService {

    @Value("${spring.ai.google.genai.api-key}")
    private String apiKey;

    private final ChatModel chatModel;
    @Scheduled(fixedRate = 1000 * 60 * 60* 24)
    private void generatePrompt() {
        log.info("Generating prompt for image generation...");
        String system = """
                Du er en AI prosjektleder som gir instrukser til AI markedsførings botter. Du skal nå skrive en prompt som får
                en AI markedsføringsbot til å lage et bilde for SaaS appen "Bilago". Bilago er et regnskapssystem
                spesielt rettet mot enkeltpersonsforetak med under 5.000.000 i omsetning.
                
                Det er viktig at du aldri ber markedsførings bottene om å lage noen ekte bilder, men heller grafisk innhold/infografikk.
                
                Bildet som blir laget skal være i 1:1 format.
                
                Det er viktig at du ikke gir noen forklaringer eller noe, da det det du skriver vil gå rett til ai botten.
                
                Outputten din skal altså være noe sånt som:
                Lag en illustrasjon av...
                
                eller
                
                Lag et profesjonelt markedsføringsbilde for...
                """;
        String prompt = chatModel.call(system);
        generateImage(prompt);
    }

    @SneakyThrows
    public void generateImage(String prompt) {
        log.info("Generating image for prompt {}", prompt);
        try (Client client = Client.builder().apiKey(apiKey).build()) {
            log.info("Generating image...");
            GenerateContentConfig contentConfig = GenerateContentConfig.builder()
                    .responseModalities("TEXT", "IMAGE")
                    .build();

            GenerateContentResponse response = client.models.generateContent(
                    "gemini-3-pro-image-preview",
                    prompt,
                    contentConfig
            );

            log.info("Received response!");

            for (Part part : response.parts()) {
                if (part.text().isPresent()) {
                    log.info("Response text: " + part.text().get());
                }
                if (part.inlineData().isPresent()) {
                    Blob blob = part.inlineData().get();
                    if (blob.data().isPresent()) {
                        Files.write(Paths.get("_01_generated_image.png"), blob.data().get());
                        log.info("Image saved!");
                    }else {
                        log.error("No blob data!");
                    }
                }else {
                    log.error("No inline data!");
                }
            }
        }
    }
}
