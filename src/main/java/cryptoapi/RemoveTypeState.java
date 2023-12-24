package cryptoapi;

public class RemoveTypeState implements ChatState {
    @Override
    public void prev(Chat chat, String str) {
        chat.setState(new RemoveCoinState());
    }

    @Override
    public void next(Chat chat, String str) {
        chat.setState(new NoneState());
    }
}
