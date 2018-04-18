package common;

public class Enums {
    public interface NewsFlag{
        int Normal = 0;   //普通新闻
        int Hot = 1;   //热门新闻
    }

    public interface QuestionType{  //客服专区提问时问题所属类型
        int Complaint = 1;   //投诉举报
        int BUG = 2;   //BUG反馈
        int Advice = 3; //玩家建议
    }

}
