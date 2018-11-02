package com.study.zk;

import com.study.zkpersistence.ZKPersistenceUtils;
import org.junit.Test;

/**
 * Created by lf52 on 2018/11/2.
 */
public class ZKPersistenceTest {

    @Test
    public void testZKPersistence() throws Exception {
        ZKPersistenceUtils utils = new ZKPersistenceUtils("localhost:2181");
        //System.out.println(utils.persistence("/hachecker", "D:\\leo_file\\work\\test.txt"));
        //System.out.println(utils.recovery("/futest", "D:\\leo_file\\work\\test.txt"));
        System.out.println(utils.deleteZkNode4Path("/futest"));
    }

}
