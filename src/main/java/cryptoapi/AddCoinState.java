package cryptoapi;

public class AddCoinState implements ChatState{
    @Override
    public void prev(Chat chat, String str) {
        chat.setState(new NoneState());
    }

    @Override
    public void next(Chat chat, String str) {
        chat.setState(new AddTypeState());
    }
}
