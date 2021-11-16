package cn.vuffy.emos.wx;

import cn.hutool.core.util.StrUtil;
import cn.vuffy.emos.wx.config.SystemConstants;
import cn.vuffy.emos.wx.db.dao.SysConfigDao;
import cn.vuffy.emos.wx.db.pojo.SysConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.List;

@SpringBootApplication
// 主类添加@ServletComponentScan注解使filter生效
@ServletComponentScan
@Slf4j
public class EmosWxApiApplication {

	@Autowired
	private SysConfigDao sysConfigDao;

	@Autowired
	private SystemConstants constants;

	public static void main(String[] args) {
		SpringApplication.run(EmosWxApiApplication.class, args);
	}

	@PostConstruct
	public void init() {
		List<SysConfig> list = sysConfigDao.selectAllParam();
		list.forEach(one -> {
			String key = one.getParamKey();
			key = StrUtil.toCamelCase(key);
			String value = one.getParamValue();
			try {
				Field field = constants.getClass().getDeclaredField(key);
				field.set(constants, value);
			} catch (Exception e) {
				log.error("执行异常", e);
			}
		});
	}

}
