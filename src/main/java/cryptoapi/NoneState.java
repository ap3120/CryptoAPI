package cryptoapi;

public class NoneState implements ChatState{
    private String stateString = "none";

    public NoneState(String str) {

    }
    @Override
    public void prev(Chat chat, String str) {
        System.out.println("The chat is in None state");
    }

    @Override
    public void next(Chat chat, String str) {
        if (str.equals("/add")) {
            chat.setState(new AddCoinState(str));
        } else {
            chat.setState(new RemoveCoinState(str));
        }
    }

    @Override
    public String getStateString() {
        return this.stateString;
    }
}
