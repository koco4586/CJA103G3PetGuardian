package com.petguardian.member.repository.management;

import com.petguardian.member.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberManagementRepository extends JpaRepository<Member,Integer> {

//    public Member save(Member member);//新增或更新單筆資料

//    public Optional<Member> findById(Integer id);//.orElse(null)//根據 ID 查詢單筆

//    public List<Member>或Member findByMemSex(Integer memSex);//自定義查詢

//    public List<Member> findAll();//查詢全部

//    public void deleteById(Integer id);//根據 ID 刪除單筆資料

}
