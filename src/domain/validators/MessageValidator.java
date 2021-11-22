package domain.validators;
import domain.Message;

public class MessageValidator implements Validator<Message> {
    @Override
    public void validate(Message message) throws ValidationException {
        if(message.getMessage() == "" || message.getMessage() == null)
        {
            throw new ValidationException("Message can't be empty or null");
        }
        if(message.getUser() == null){
            throw new ValidationException("User id must not null");
        }
        if(message.getReplyId() == null){
            throw new ValidationException("The id of the reply can't be null");
        }
    }
}
