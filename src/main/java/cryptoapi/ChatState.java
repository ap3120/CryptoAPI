package cryptoapi;

public interface ChatState {
    String stateString = "";
    void prev(Chat chat, String str);
    void next(Chat chat, String str);
    String getStateString();
}
