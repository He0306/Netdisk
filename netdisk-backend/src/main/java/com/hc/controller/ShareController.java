package com.hc.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hc.annotation.GlobalInterceptor;
import com.hc.annotation.VerifyParam;
import com.hc.common.lang.Constants;
import com.hc.common.lang.Result;
import com.hc.entity.Share;
import com.hc.entity.dto.SessionWebUserDto;
import com.hc.entity.vo.SharePageVo;
import com.hc.service.ShareService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

/**
 * @author: hec
 * @date: 2023-10-22
 * @email: 2740860037@qq.com
 * @description: 分享控制器
 */
@RestController
@RequestMapping("/share")
@Api(tags = "分享相关")
public class ShareController {

    @Autowired
    ShareService shareService;

    /**
     * 分页查询分享
     *
     * @param session
     * @param pageNo
     * @param pageSize
     * @return
     */
    @ApiOperation(value = "分页查询分享")
    @PostMapping("/loadShareList")
    @GlobalInterceptor
    public Result loadShareList(HttpSession session, Integer pageNo, Integer pageSize) {
        SessionWebUserDto sessionWebUserDto = (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
        IPage<Share> sharePageList = shareService.findShareListByPage(sessionWebUserDto.getUserId(), pageNo, pageSize);
        SharePageVo sharePageVo = new SharePageVo();
        sharePageVo.setList(sharePageList.getRecords());
        sharePageVo.setPageNo(Long.valueOf(pageNo));
        sharePageVo.setPageSize(Long.valueOf(pageSize));
        sharePageVo.setPageTotal(sharePageList.getCurrent());
        sharePageVo.setTotalCount(sharePageList.getTotal());
        return Result.success(sharePageVo);
    }

    /**
     * 新增分享
     *
     * @param session
     * @param fileId
     * @param validType
     * @param code
     * @return
     */
    @ApiOperation(value = "新增分享")
    @PostMapping("/shareFile")
    @GlobalInterceptor(checkParams = true)
    public Result shareFile(HttpSession session,
                            @VerifyParam(required = true) String fileId,
                            @VerifyParam(required = true) Integer validType,
                            String code) {
        SessionWebUserDto sessionWebUserDto = (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
        Share share = new Share();
        share.setUserId(sessionWebUserDto.getUserId());
        share.setCode(code);
        share.setFileId(fileId);
        share.setValidType(validType);
        shareService.saveShare(share);
        return Result.success(share);
    }

    /**
     * 取消分享
     *
     * @param session
     * @param shareIds
     * @return
     */
    @ApiOperation(value = "取消分享")
    @PostMapping("/cancelShare")
    @GlobalInterceptor(checkParams = true)
    public Result cancelShare(HttpSession session, @VerifyParam(required = true) String shareIds) {
        SessionWebUserDto sessionWebUserDto = (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
        shareService.deleteFileShareBatch(shareIds.split(","), sessionWebUserDto.getUserId());
        return Result.success();
    }
}
