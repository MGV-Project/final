package kr.co.mgv.sample.service;

import kr.co.mgv.sample.dao.SampleDao;
import kr.co.mgv.sample.vo.TestVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SampleService {

    private final SampleDao sampleDao;

    public TestVo getTest() {
        return sampleDao.getTest().orElseThrow();
    }

}
