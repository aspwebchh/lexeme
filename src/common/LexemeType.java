package common;

public enum LexemeType {
    UserMachineChat,
    RewardQuestionGameOne,
    RewardQuestionGameTwo,
    RewardQuestionGameThree;

    public static LexemeType getLexTypeByGameId(int gameId ) {
        LexemeType lexType;
        switch (gameId)
        {
            case 1 : lexType = LexemeType.RewardQuestionGameOne; break;
            case 2 : lexType = LexemeType.RewardQuestionGameTwo; break;
            case 3 : lexType = LexemeType.RewardQuestionGameThree; break;
            default: lexType = LexemeType.RewardQuestionGameOne; break;
        }
        return lexType;
    }
}
