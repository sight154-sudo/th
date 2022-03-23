package com.tanhua.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tanhua.common.mapper.QuestionMapper;
import com.tanhua.common.pojo.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: tang
 * @date: Create in 17:36 2021/8/17
 * @description:
 */
@Service
public class QuestionService {

    @Autowired
    private QuestionMapper questionMapper;
    public void updateQuestion(Question question){
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getUserId,question.getUserId());
        this.questionMapper.update(question,wrapper);
    }

    public Question queryQuestion(Long userId){
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getUserId,userId);
        return questionMapper.selectOne(wrapper);
    }

    public void insertQuestion(Question question){
        this.questionMapper.insert(question);
    }

}
