package cryptoapi;

public interface ChatState {
    void prev(Chat chat, String str);
    void next(Chat chat, String str);
}
