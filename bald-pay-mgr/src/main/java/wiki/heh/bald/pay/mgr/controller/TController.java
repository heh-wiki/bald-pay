package wiki.heh.bald.pay.mgr.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import wiki.heh.bald.pay.mgr.model.vo.Result;

import java.util.HashMap;

/**
 * @author heh
 * @date 2020/12/2
 */
@RestController
public class TController {

    @GetMapping("test")
    public Result t(@RequestParam HashMap<String, Object> o) {
        System.out.println("******************PostMapping***********************************\n");
        System.out.println(o.toString());
        System.out.println("\n******************PostMapping***********************************");
        return Result.success();
    }

    @PostMapping("test")
    public Result post(@RequestParam HashMap<String, Object> o) {
        System.out.println("******************PostMapping***********************************\n");
        System.out.println(o.toString());
        System.out.println("\n******************PostMapping***********************************");
        return Result.success();
    }
}
