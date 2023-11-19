package com.hc.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hc.annotation.GlobalInterceptor;
import com.hc.annotation.VerifyParam;
import com.hc.common.lang.Constants;
import com.hc.common.lang.Result;
import com.hc.entity.FileInfo;
import com.hc.entity.dto.SessionWebUserDto;
import com.hc.entity.vo.FileInfoPageVO;
import com.hc.service.FileInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

/**
 * @author: hec
 * @date: 2023-10-08
 * @email: 2740860037@qq.com
 * @description: 回收站控制器
 */
@RestController
@RequestMapping("/recycle")
public class RecycleController {

    @Autowired
    FileInfoService fileInfoService;

    /**
     * 分页查询FileInfo 回收站 全部数据
     *
     * @param session
     * @param pageNo
     * @param pageSize
     * @return
     */
    @PostMapping("/loadRecycleList")
    @GlobalInterceptor
    public Result loadRecycle(HttpSession session, Integer pageNo, Integer pageSize) {
        SessionWebUserDto sessionWebUserDto = (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
        IPage<FileInfo> fileInfoIPage = fileInfoService.findRecycleListByPage(sessionWebUserDto.getUserId(), pageNo, pageSize);
        FileInfoPageVO fileInfoPageVO = new FileInfoPageVO();
        fileInfoPageVO.setList(fileInfoIPage.getRecords());
        fileInfoPageVO.setPageNo(Long.valueOf(pageNo));
        fileInfoPageVO.setPageSize(Long.valueOf(pageSize));
        fileInfoPageVO.setPageTotal(fileInfoIPage.getCurrent());
        fileInfoPageVO.setTotalCount(fileInfoIPage.getTotal());
        return Result.success(fileInfoPageVO);
    }

    /**
     * 还原
     *
     * @param session
     * @param fileIds
     * @return
     */
    @PostMapping("/recoverFile")
    @GlobalInterceptor
    public Result recoverFile(HttpSession session, @VerifyParam(required = true) String fileIds) {
        SessionWebUserDto sessionWebUserDto = (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
        fileInfoService.recoverFileBatch(sessionWebUserDto.getUserId(), fileIds);
        return Result.success();
    }

    /**
     * 彻底删除
     *
     * @param session
     * @param fileIds
     * @return
     */
    @PostMapping("/delFile")
    @GlobalInterceptor
    public Result delFile(HttpSession session, @VerifyParam(required = true) String fileIds) {
        SessionWebUserDto sessionWebUserDto = (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
        fileInfoService.delFileBatch(sessionWebUserDto.getUserId(), fileIds,false);
        return Result.success();
    }

}
