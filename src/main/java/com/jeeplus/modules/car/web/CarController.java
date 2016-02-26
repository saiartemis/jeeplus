/**
 * Copyright &copy; 2015-2020 <a href="--版权信息--">JeePlus</a> All rights reserved.
 */
package com.jeeplus.modules.car.web;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.collect.Lists;
import com.jeeplus.common.utils.DateUtils;
import com.jeeplus.common.config.Global;
import com.jeeplus.common.persistence.Page;
import com.jeeplus.common.web.BaseController;
import com.jeeplus.common.utils.StringUtils;
import com.jeeplus.common.utils.excel.ExportExcel;
import com.jeeplus.common.utils.excel.ImportExcel;
import com.jeeplus.modules.car.entity.Car;
import com.jeeplus.modules.car.service.CarService;

/**
 * 1Controller
 * @author zx
 * @version 2016-02-25
 */
@Controller
@RequestMapping(value = "${adminPath}/car/car")
public class CarController extends BaseController {

	@Autowired
	private CarService carService;
	
	@ModelAttribute
	public Car get(@RequestParam(required=false) String id) {
		Car entity = null;
		if (StringUtils.isNotBlank(id)){
			entity = carService.get(id);
		}
		if (entity == null){
			entity = new Car();
		}
		return entity;
	}
	
	/**
	 * 1列表页面
	 */
	@RequiresPermissions("car:car:list")
	@RequestMapping(value = {"list", ""})
	public String list(Car car, HttpServletRequest request, HttpServletResponse response, Model model) {
		Page<Car> page = carService.findPage(new Page<Car>(request, response), car); 
		model.addAttribute("page", page);
		return "modules/car/carList";
	}

	/**
	 * 查看，增加，编辑1表单页面
	 */
	@RequiresPermissions(value={"car:car:view","car:car:add","car:car:edit"},logical=Logical.OR)
	@RequestMapping(value = "form")
	public String form(Car car, Model model) {
		model.addAttribute("car", car);
		return "modules/car/carForm";
	}

	/**
	 * 保存1
	 */
	@RequiresPermissions(value={"car:car:add","car:car:edit"},logical=Logical.OR)
	@RequestMapping(value = "save")
	public String save(Car car, Model model, RedirectAttributes redirectAttributes) {
		if (!beanValidator(model, car)){
			return form(car, model);
		}
		carService.save(car);
		addMessage(redirectAttributes, "保存1成功");
		return "redirect:"+Global.getAdminPath()+"/car/car/?repage";
	}
	
	/**
	 * 删除1
	 */
	@RequiresPermissions("car:car:del")
	@RequestMapping(value = "delete")
	public String delete(Car car, RedirectAttributes redirectAttributes) {
		carService.delete(car);
		addMessage(redirectAttributes, "删除1成功");
		return "redirect:"+Global.getAdminPath()+"/car/car/?repage";
	}
	
	/**
	 * 批量删除1
	 */
	@RequiresPermissions("car:car:del")
	@RequestMapping(value = "deleteAll")
	public String deleteAll(String ids, RedirectAttributes redirectAttributes) {
		String idArray[] =ids.split(",");
		for(String id : idArray){
			carService.delete(carService.get(id));
		}
		addMessage(redirectAttributes, "删除1成功");
		return "redirect:"+Global.getAdminPath()+"/car/car/?repage";
	}
	
	/**
	 * 导出excel文件
	 */
	@RequiresPermissions("car:car:export")
    @RequestMapping(value = "export", method=RequestMethod.POST)
    public String exportFile(Car car, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes) {
		try {
            String fileName = "1"+DateUtils.getDate("yyyyMMddHHmmss")+".xlsx";
            Page<Car> page = carService.findPage(new Page<Car>(request, response, -1), car);
    		new ExportExcel("1", Car.class).setDataList(page.getList()).write(response, fileName).dispose();
    		return null;
		} catch (Exception e) {
			addMessage(redirectAttributes, "导出1记录失败！失败信息："+e.getMessage());
		}
		return "redirect:"+Global.getAdminPath()+"/car/car/?repage";
    }

	/**
	 * 导入Excel数据

	 */
	@RequiresPermissions("car:car:import")
    @RequestMapping(value = "import", method=RequestMethod.POST)
    public String importFile(MultipartFile file, RedirectAttributes redirectAttributes) {
		try {
			int successNum = 0;
			ImportExcel ei = new ImportExcel(file, 1, 0);
			List<Car> list = ei.getDataList(Car.class);
			for (Car car : list){
				carService.save(car);
			}
			addMessage(redirectAttributes, "已成功导入 "+successNum+" 条1记录");
		} catch (Exception e) {
			addMessage(redirectAttributes, "导入1失败！失败信息："+e.getMessage());
		}
		return "redirect:"+Global.getAdminPath()+"/car/car/?repage";
    }
	
	/**
	 * 下载导入1数据模板
	 */
	@RequiresPermissions("car:car:import")
    @RequestMapping(value = "import/template")
    public String importFileTemplate(HttpServletResponse response, RedirectAttributes redirectAttributes) {
		try {
            String fileName = "1数据导入模板.xlsx";
    		List<Car> list = Lists.newArrayList(); 
    		new ExportExcel("1数据", Car.class, 1).setDataList(list).write(response, fileName).dispose();
    		return null;
		} catch (Exception e) {
			addMessage(redirectAttributes, "导入模板下载失败！失败信息："+e.getMessage());
		}
		return "redirect:"+Global.getAdminPath()+"/car/car/?repage";
    }
	

}