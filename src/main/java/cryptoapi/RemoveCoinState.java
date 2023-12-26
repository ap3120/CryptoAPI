package cryptoapi;

public class RemoveCoinState implements ChatState {
    private String stateString;

    public RemoveCoinState(String str) {
        this.stateString = str;
    }
    @Override
    public void prev(Chat chat, String str) {
        chat.setState(new NoneState(""));
    }

    @Override
    public void next(Chat chat, String str) {
        chat.setState(new RemoveTypeState(this.stateString + str));
    }

    @Override
    public String getStateString() {
        return this.stateString;
    }
}
