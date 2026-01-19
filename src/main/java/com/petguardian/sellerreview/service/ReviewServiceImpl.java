package com.petguardian.sellerreview.service;

import com.petguardian.sellerreview.model.*;
import com.petguardian.orders.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private SellerReviewRepository sellerReviewRepository;

    @Autowired
    private OrdersRepository ordersRepository;

    @Override
    public SellerReviewVO addReview(SellerReviewVO review) {
        return sellerReviewRepository.save(review);
    }

    @Override
    public List<SellerReviewVO> getSellerReviews(Integer sellerMemId) {
        // 假設 Repository 有這個方法（讓其他同學實作）
        // 如果沒有，請他們加上：
        // List<SellerReview> findByOrder_SellerMemIdAndShowStatusOrderByReviewTimeDesc(Integer sellerMemId, Integer showStatus);
        return sellerReviewRepository.findAll(); // 暫時用 findAll，實際要過濾
    }

    @Override
    public List<SellerReviewVO> getBuyerReviews(Integer buyerMemId) {
        // 假設 Repository 有這個方法
        return sellerReviewRepository.findAll(); // 暫時用 findAll
    }

    @Override
    public void updateMemberRating(Integer memId) {
        // TODO: 計算該會員的平均評分，更新到 member 表
        // 需要取得所有評價，計算平均值
    }
}
