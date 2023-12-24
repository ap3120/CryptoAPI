package cryptoapi;

public class AddTargetState implements ChatState {
    @Override
    public void prev(Chat chat, String str) {
        chat.setState(new AddTargetState());
    }

    @Override
    public void next(Chat chat, String str) {
        chat.setState(new NoneState());
    }
}
