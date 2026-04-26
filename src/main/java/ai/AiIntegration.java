/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ai;

/**
 *
 * @author k2
 */
import com.google.genai.Chat;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.Part;
import com.mycompany.rrsalesandinventory.AppConfig;


public class AiIntegration {
    private final String API_KEY = AppConfig.getAiApiKey();
    private final Client client = Client.builder().apiKey(API_KEY).build();
    // The "Memory" of the bot
    private final Chat chatSession;
    
    public AiIntegration() {
    // 1. Set the Persona (System Instructions)
        String instructions = """
            You are part of a sales and inventory system. Answer only about the data 
            given to you about it (sales reports and invenotory stocks).This data will be passed in to you
            when the user prompts(hidden to them), if you don't get the data just work with what you have.
            And if the user asks anything out of scope, just tell them that it is out of scope.
        """;

        // 2. Wrap instructions into a Config object
        GenerateContentConfig config = GenerateContentConfig.builder().systemInstruction(Content.fromParts(Part.fromText(instructions))).build();

        // 3. Start a session that will remember the conversation
        this.chatSession = client.chats.create("gemini-2.5-flash-lite",config);
    }

    public String getAIResponse(String userPrompt) {
        String result;

        try {
            result = chatSession.sendMessage(userPrompt).text();
        } catch (Exception e) {
            result = "Connection Error: " + e.getMessage();
        }
        return result;
    }
}
