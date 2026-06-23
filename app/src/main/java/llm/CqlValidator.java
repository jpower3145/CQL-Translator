package llm;

public class CqlValidator {

    /**
     * Validates basic structural rules of the #LancsBox query syntax.
     */
    public static boolean isValid(String cqlQuery) {
        if (cqlQuery == null || cqlQuery.trim().isEmpty()) {
            return false;
        }

        String trimmed = cqlQuery.trim();

        // Rule 1: A valid basic CQL segment must open with '[' and close with ']'
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
            return false;
        }

        // Rule 2: It should contain a valid attribute key definition (e.g., pos, word, sem, hw)
        if (!trimmed.contains("pos=") && !trimmed.contains("word=") && !trimmed.contains("sem=") && !trimmed.contains("hw=")) {
            return false;
        }

        return true;
    }
}