package kr.co.mgv.sample.controller;


import kr.co.mgv.sample.service.SampleService;
import kr.co.mgv.sample.vo.TestVo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sample")
@RequiredArgsConstructor
public class SampleController {

    private final SampleService sampleService;

    @GetMapping("/get")
    public ResponseEntity<TestVo> getTest() {
        return ResponseEntity.ok(sampleService.getTest());
    }

}
