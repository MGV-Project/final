package kr.co.mgv.sample.dao;

import kr.co.mgv.sample.vo.TestVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Mapper
@Repository
public interface SampleDao {
    Optional<TestVo> getTest();
}
