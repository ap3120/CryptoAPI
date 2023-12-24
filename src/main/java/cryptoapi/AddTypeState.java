package cryptoapi;

public class AddTypeState implements ChatState {
    @Override
    public void prev(Chat chat, String str) {
        chat.setState(new AddCoinState());
    }

    @Override
    public void next(Chat chat, String str) {
        chat.setState(new AddTargetState());
    }
}
