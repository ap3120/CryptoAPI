package cryptoapi;

public class AddTypeState implements ChatState {
    private String stateString;

    public AddTypeState(String str) {
        this.stateString = str;
    }
    @Override
    public void prev(Chat chat, String str) {
        chat.setState(new AddCoinState(""));
    }

    @Override
    public void next(Chat chat, String str) {
        chat.setState(new AddTargetState(this.stateString + str));
    }

    @Override
    public String getStateString() {
        return this.stateString;
    }
}
