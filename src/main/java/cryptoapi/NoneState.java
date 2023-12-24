package cryptoapi;

public class NoneState implements ChatState{
    @Override
    public void prev(Chat chat, String str) {
        System.out.println("The chat is in None state");
    }

    @Override
    public void next(Chat chat, String str) {
        if (str.equals("add")) {
            chat.setState(new AddCoinState());
        } else {
            chat.setState(new RemoveCoinState());
        }
    }
}
