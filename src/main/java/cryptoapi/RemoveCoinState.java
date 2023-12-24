package cryptoapi;

public class RemoveCoinState implements ChatState {
    @Override
    public void prev(Chat chat, String str) {
        chat.setState(new NoneState());
    }

    @Override
    public void next(Chat chat, String str) {
        chat.setState(new RemoveTypeState());
    }
}
