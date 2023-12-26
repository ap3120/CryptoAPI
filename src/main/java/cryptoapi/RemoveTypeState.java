package cryptoapi;

public class RemoveTypeState implements ChatState {
    private String stateString;

    public RemoveTypeState(String str) {
        this.stateString = str;
    }
    @Override
    public void prev(Chat chat, String str) {
        chat.setState(new RemoveCoinState(""));
    }

    @Override
    public void next(Chat chat, String str) {
        chat.setState(new NoneState(""));
    }

    @Override
    public String getStateString() {
        return this.stateString;
    }
}
