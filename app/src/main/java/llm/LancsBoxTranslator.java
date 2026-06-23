package llm;

import dev.langchain4j.model.ollama.OllamaChatModel;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class LancsBoxTranslator {
    /**
     * Teaches LLM the rules so we can ask new questions
     */
    private final OllamaChatModel model;
    private final Gson gson;

    public LancsBoxTranslator() {
        this.model = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("phi3") 
                .temperature(0.0) // Keeps the model focused and non-creative due to rigid task nature
                .build();
        this.gson = new Gson();
    }

    public TranslationResult translateToCql(String userQuestion) {
        // A prompt that defines the grammar rules. Hallucinated SQL queries without elaboration.
        String systemPrompt = "You are a specialized compiler that translates English text into #LancsBox Corpus Query Language (CQL).\n" +
                "CRITICAL SYSTEM RULES:\n" +
                "1. Semantic related queries should use the USAS Semantic Tagset\n" +
                "2. Double equals (==) means case-sensitive is a necessity. Example: no-case sensitivity: [word=\"LaTeX\"] becomes case-sensitive: [word==\"LaTeX\"].\n" +
                "3. Use standard CLAWS7 POS tags (J.* for Adjective, N.* for Noun, V.* for Verb).\n" +
                "4. Your output must be a clean JSON object with exactly two keys: 'cql' and 'explanation'.\n" +
                "Compilation Examples:\n\n" +
                
                "Input: How frequent are nouns?\n" +
                "Output: {\"cql\": \"[pos=\\\"N.*\\\"]\", \"explanation\": \"Retrieve all tokens tagged as nouns, i.e. starting with N.\"}\n\n" +
                
                "Input: Find words starting with jump but not ending in ing\n" +
                "Output: {\"cql\": \"[word=\\\"jump.*\\\" word!=\\\".*ing\\\"]\", \"explanation\": \"Matches words starting with 'jump' and excludes those ending in 'ing'.\"}\n\n" +
                
                "Input: Find words with the headword like\n" +
                "Output: {\"cql\": \"[hw=\\\"like\\\"]\", \"explanation\": \"Retrieve all forms of the headword 'like'.\"}\n\n" +
                
                "Input: Match professional as an adjective\n" +
                "Output: {\"cql\": \"[word=\\\"professional\\\" pos=\\\"J.*\\\"]\", \"explanation\": \"Retrieve occurrences of 'professional' tagged specifically as an adjective.\"}\n\n" +
                
                "Input: Find food-related verbs or nouns\n" +
                "Output: {\"cql\": \"[sem=\\\"F1\\\" (pos=\\\"V.*\\\" | pos=\\\"N.*\\\")]\", \"explanation\": \"Filter by USAS semantic category F1 (food) matching either verb or noun tags.\"}\n\n" +
                
                "Input: Find any word after green or blue\n" +
                "Output: {\"cql\": \"[word=\\\"green|blue\\\"][]\", \"explanation\": \"Matches 'green' or 'blue' followed immediately by any single token wildcard.\"}\n\n" +                
                
                "Input: " + userQuestion + "\n" +
                "Output:";

        String rawResponse = model.generate(systemPrompt);
        
        // Raw output to help format JSON afterwards
        System.out.println("\n[DEBUG] Raw LLM Output:\n" + rawResponse);

        // Sanitize the text before parsing
        String cleanJson = sanitizeJsonOutput(rawResponse);

        try {
            return gson.fromJson(cleanJson, TranslationResult.class);
        } catch (JsonSyntaxException e) {
            // Ultimate safety fallback layer with debug error print
            System.out.println("[DEBUG] JSON Parser crashed: " + e.getMessage());
            TranslationResult errorResult = new TranslationResult();
            errorResult.setCql("[word=\"" + userQuestion + "\"]");
            errorResult.setExplanation("Fallback: Generated due to a format parsing variance.");
            return errorResult;
        }
    }

    /**
     * Extracts only the raw JSON object from the LLM response,
     * stripping out irrelevant noise
     */
    private String sanitizeJsonOutput(String rawText) {
        if (rawText == null) return "{}";
        
        String clean = rawText.trim();
        
        // Fix LLM over-escaping backslashes before quotes (converts \\" to \")
        clean = clean.replace("\\\\\"", "\\\"");
        
        // Remove markdown wrappers if present to not rely on prompt fully
        if (clean.contains("```json")) {
            clean = clean.substring(clean.indexOf("```json") + 7);
        } else if (clean.contains("```")) {
            clean = clean.substring(clean.indexOf("```") + 3);
        }
        
        if (clean.contains("```")) {
            clean = clean.substring(0, clean.indexOf("```"));
        }
        
        // Find the absolute boundaries of the JSON object
        int firstBrace = clean.indexOf("{");
        int lastBrace = clean.lastIndexOf("}");
        
        // Extract only JSON substring
        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            clean = clean.substring(firstBrace, lastBrace + 1);
        }
        
        return clean.trim();
    }
}