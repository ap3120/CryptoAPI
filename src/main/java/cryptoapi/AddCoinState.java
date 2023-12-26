package cryptoapi;

public class AddCoinState implements ChatState {
    private String stateString;

    public AddCoinState(String str) {
        this.stateString = str;
    }
    @Override
    public void prev(Chat chat, String str) {
        chat.setState(new NoneState(""));
    }

    @Override
    public void next(Chat chat, String str) {
        chat.setState(new AddTypeState(this.stateString + str));
    }

    @Override
    public String getStateString() {
        return this.stateString;
    }
}
