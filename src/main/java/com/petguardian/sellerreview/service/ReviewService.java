package com.petguardian.sellerreview.service;

import com.petguardian.sellerreview.model.*;
import java.util.List;

public interface ReviewService {

    // 買家新增評價
    SellerReviewVO addReview(SellerReviewVO review);

    // 查詢賣家的所有評價
    List<SellerReviewVO> getSellerReviews(Integer sellerMemId);

    // 查詢買家的評價
    List<SellerReviewVO> getBuyerReviews(Integer buyerMemId);

    // 更新會員的評價分數
    void updateMemberRating(Integer memId);
}