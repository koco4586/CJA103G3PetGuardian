package com.petguardian.sitter.service;

import java.util.List;
import com.petguardian.sitter.model.SitterSearchCriteria;
import com.petguardian.sitter.model.SitterSearchDTO;

public interface SitterSearchService {

    /**
     * 根據條件搜尋保姆
     * 
     * @param criteria 搜尋條件
     * @return List<SitterSearchDTO> 符合條件的保姆列表
     */
    List<SitterSearchDTO> searchSitters(SitterSearchCriteria criteria);

    /**
     * 取得所有啟用中的保姆（用於無篩選條件時）
     * 
     * @return List<SitterSearchDTO> 所有啟用中的保姆列表
     */
    List<SitterSearchDTO> getAllActiveSitters();
}
