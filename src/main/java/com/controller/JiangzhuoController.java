
package com.controller;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSONObject;
import java.util.*;
import org.springframework.beans.BeanUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import com.service.TokenService;
import com.utils.*;
import java.lang.reflect.InvocationTargetException;

import com.service.DictionaryService;
import org.apache.commons.lang3.StringUtils;
import com.annotation.IgnoreAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.entity.*;
import com.entity.view.*;
import com.service.*;
import com.utils.PageUtils;
import com.utils.R;
import com.alibaba.fastjson.*;

/**
 * 讲座信息
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/jiangzhuo")
public class JiangzhuoController {
    private static final Logger logger = LoggerFactory.getLogger(JiangzhuoController.class);

    private static final String TABLE_NAME = "jiangzhuo";

    @Autowired
    private JiangzhuoService jiangzhuoService;


    @Autowired
    private TokenService tokenService;
    @Autowired
    private DictionaryService dictionaryService;

    //级联表非注册的service
    //注册表service
    @Autowired
    private YonghuService yonghuService;
    @Autowired
    private XinliyishengService xinliyishengService;


    /**
    * 后端列表
    */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永不会进入");
        else if("用户".equals(role))
            params.put("yonghuId",request.getSession().getAttribute("userId"));
        else if("心理医生".equals(role))
            params.put("xinliyishengId",request.getSession().getAttribute("userId"));
        params.put("jiangzhuoDeleteStart",1);params.put("jiangzhuoDeleteEnd",1);
        CommonUtil.checkMap(params);
        PageUtils page = jiangzhuoService.queryPage(params);

        //字典表数据转换
        List<JiangzhuoView> list =(List<JiangzhuoView>)page.getList();
        for(JiangzhuoView c:list){
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c, request);
        }
        return R.ok().put("data", page);
    }

    /**
    * 后端详情
    */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("info方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        JiangzhuoEntity jiangzhuo = jiangzhuoService.selectById(id);
        if(jiangzhuo !=null){
            //entity转view
            JiangzhuoView view = new JiangzhuoView();
            BeanUtils.copyProperties( jiangzhuo , view );//把实体数据重构到view中
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view, request);
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }

    }

    /**
    * 后端保存
    */
    @RequestMapping("/save")
    public R save(@RequestBody JiangzhuoEntity jiangzhuo, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,jiangzhuo:{}",this.getClass().getName(),jiangzhuo.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永远不会进入");

        Wrapper<JiangzhuoEntity> queryWrapper = new EntityWrapper<JiangzhuoEntity>()
            .eq("jiangzhuo_name", jiangzhuo.getJiangzhuoName())
            .eq("jiangzhuo_types", jiangzhuo.getJiangzhuoTypes())
            .eq("jiangzhuo_shichang", jiangzhuo.getJiangzhuoShichang())
            .eq("jiangzhuo_address", jiangzhuo.getJiangzhuoAddress())
            .eq("jiangzhuo_delete", jiangzhuo.getJiangzhuoDelete())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        JiangzhuoEntity jiangzhuoEntity = jiangzhuoService.selectOne(queryWrapper);
        if(jiangzhuoEntity==null){
            jiangzhuo.setJiangzhuoDelete(1);
            jiangzhuo.setInsertTime(new Date());
            jiangzhuo.setCreateTime(new Date());
            jiangzhuoService.insert(jiangzhuo);
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody JiangzhuoEntity jiangzhuo, HttpServletRequest request) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        logger.debug("update方法:,,Controller:{},,jiangzhuo:{}",this.getClass().getName(),jiangzhuo.toString());
        JiangzhuoEntity oldJiangzhuoEntity = jiangzhuoService.selectById(jiangzhuo.getId());//查询原先数据

        String role = String.valueOf(request.getSession().getAttribute("role"));
//        if(false)
//            return R.error(511,"永远不会进入");
        //根据字段查询是否有相同数据
        Wrapper<JiangzhuoEntity> queryWrapper = new EntityWrapper<JiangzhuoEntity>()
            .notIn("id",jiangzhuo.getId())
            .andNew()
            .eq("jiangzhuo_name", jiangzhuo.getJiangzhuoName())
            .eq("jiangzhuo_types", jiangzhuo.getJiangzhuoTypes())
            .eq("jiangzhuo_shichang", jiangzhuo.getJiangzhuoShichang())
            .eq("jiangzhuo_address", jiangzhuo.getJiangzhuoAddress())
            .eq("jiangzhuo_delete", jiangzhuo.getJiangzhuoDelete())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        JiangzhuoEntity jiangzhuoEntity = jiangzhuoService.selectOne(queryWrapper);
        if("".equals(jiangzhuo.getJiangzhuoPhoto()) || "null".equals(jiangzhuo.getJiangzhuoPhoto())){
                jiangzhuo.setJiangzhuoPhoto(null);
        }
        if(jiangzhuoEntity==null){
            jiangzhuoService.updateById(jiangzhuo);//根据id更新
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }



    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids, HttpServletRequest request){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        List<JiangzhuoEntity> oldJiangzhuoList =jiangzhuoService.selectBatchIds(Arrays.asList(ids));//要删除的数据
        ArrayList<JiangzhuoEntity> list = new ArrayList<>();
        for(Integer id:ids){
            JiangzhuoEntity jiangzhuoEntity = new JiangzhuoEntity();
            jiangzhuoEntity.setId(id);
            jiangzhuoEntity.setJiangzhuoDelete(2);
            list.add(jiangzhuoEntity);
        }
        if(list != null && list.size() >0){
            jiangzhuoService.updateBatchById(list);
        }

        return R.ok();
    }


    /**
     * 批量上传
     */
    @RequestMapping("/batchInsert")
    public R save( String fileName, HttpServletRequest request){
        logger.debug("batchInsert方法:,,Controller:{},,fileName:{}",this.getClass().getName(),fileName);
        Integer yonghuId = Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId")));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            List<JiangzhuoEntity> jiangzhuoList = new ArrayList<>();//上传的东西
            Map<String, List<String>> seachFields= new HashMap<>();//要查询的字段
            Date date = new Date();
            int lastIndexOf = fileName.lastIndexOf(".");
            if(lastIndexOf == -1){
                return R.error(511,"该文件没有后缀");
            }else{
                String suffix = fileName.substring(lastIndexOf);
                if(!".xls".equals(suffix)){
                    return R.error(511,"只支持后缀为xls的excel文件");
                }else{
                    URL resource = this.getClass().getClassLoader().getResource("static/upload/" + fileName);//获取文件路径
                    File file = new File(resource.getFile());
                    if(!file.exists()){
                        return R.error(511,"找不到上传文件，请联系管理员");
                    }else{
                        List<List<String>> dataList = PoiUtil.poiImport(file.getPath());//读取xls文件
                        dataList.remove(0);//删除第一行，因为第一行是提示
                        for(List<String> data:dataList){
                            //循环
                            JiangzhuoEntity jiangzhuoEntity = new JiangzhuoEntity();
//                            jiangzhuoEntity.setJiangzhuoName(data.get(0));                    //讲座标题 要改的
//                            jiangzhuoEntity.setJiangzhuoTypes(Integer.valueOf(data.get(0)));   //讲座类型 要改的
//                            jiangzhuoEntity.setJiangzhuoPhoto("");//详情和图片
//                            jiangzhuoEntity.setJiangzhuoShichang(data.get(0));                    //讲座时长 要改的
//                            jiangzhuoEntity.setJiangzhuoTime(sdf.parse(data.get(0)));          //开始时间 要改的
//                            jiangzhuoEntity.setJiangzhuoAddress(data.get(0));                    //讲座地址 要改的
//                            jiangzhuoEntity.setJiangzhuoContent("");//详情和图片
//                            jiangzhuoEntity.setJiangzhuoDelete(1);//逻辑删除字段
//                            jiangzhuoEntity.setInsertTime(date);//时间
//                            jiangzhuoEntity.setCreateTime(date);//时间
                            jiangzhuoList.add(jiangzhuoEntity);


                            //把要查询是否重复的字段放入map中
                        }

                        //查询是否重复
                        jiangzhuoService.insertBatch(jiangzhuoList);
                        return R.ok();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return R.error(511,"批量插入数据异常，请联系管理员");
        }
    }





    /**
    * 前端列表
    */
    @IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("list方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));

        CommonUtil.checkMap(params);
        PageUtils page = jiangzhuoService.queryPage(params);

        //字典表数据转换
        List<JiangzhuoView> list =(List<JiangzhuoView>)page.getList();
        for(JiangzhuoView c:list)
            dictionaryService.dictionaryConvert(c, request); //修改对应字典表字段

        return R.ok().put("data", page);
    }

    /**
    * 前端详情
    */
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("detail方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        JiangzhuoEntity jiangzhuo = jiangzhuoService.selectById(id);
            if(jiangzhuo !=null){


                //entity转view
                JiangzhuoView view = new JiangzhuoView();
                BeanUtils.copyProperties( jiangzhuo , view );//把实体数据重构到view中

                //修改对应字典表字段
                dictionaryService.dictionaryConvert(view, request);
                return R.ok().put("data", view);
            }else {
                return R.error(511,"查不到数据");
            }
    }


    /**
    * 前端保存
    */
    @RequestMapping("/add")
    public R add(@RequestBody JiangzhuoEntity jiangzhuo, HttpServletRequest request){
        logger.debug("add方法:,,Controller:{},,jiangzhuo:{}",this.getClass().getName(),jiangzhuo.toString());
        Wrapper<JiangzhuoEntity> queryWrapper = new EntityWrapper<JiangzhuoEntity>()
            .eq("jiangzhuo_name", jiangzhuo.getJiangzhuoName())
            .eq("jiangzhuo_types", jiangzhuo.getJiangzhuoTypes())
            .eq("jiangzhuo_shichang", jiangzhuo.getJiangzhuoShichang())
            .eq("jiangzhuo_address", jiangzhuo.getJiangzhuoAddress())
            .eq("jiangzhuo_delete", jiangzhuo.getJiangzhuoDelete())
            ;
        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        JiangzhuoEntity jiangzhuoEntity = jiangzhuoService.selectOne(queryWrapper);
        if(jiangzhuoEntity==null){
            jiangzhuo.setJiangzhuoDelete(1);
            jiangzhuo.setInsertTime(new Date());
            jiangzhuo.setCreateTime(new Date());
        jiangzhuoService.insert(jiangzhuo);

            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

}
