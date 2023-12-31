package cryptoapi;

public class Chat {
    private String chatId;
    private ChatState chatState = new NoneState("");

    public Chat(String chatId) {
        this.chatId = chatId;
    }

    public <T extends ChatState> T getState() {
        return (T) this.chatState;
    }

    public <T extends ChatState> void setState(T newState) {
        this.chatState = newState;
    }

    public void previousState(String str) {
        chatState.prev(this, str);
    }

    public void nextState(String str) {
        chatState.next(this, str);
    }
}
