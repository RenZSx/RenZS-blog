package com.chen.blog.module.photo.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chen.blog.module.photo.dao.PhotoDao;
import com.chen.blog.common.exception.BizException;
import com.chen.blog.module.photo.dto.PhotoBackDTO;
import com.chen.blog.module.photo.dto.PhotoDTO;
import com.chen.blog.module.photo.dto.PhotoListDTO;
import com.chen.blog.module.photo.entity.Photo;
import com.chen.blog.module.photo.entity.PhotoAlbum;
import com.chen.blog.module.photo.service.PhotoAlbumService;
import com.chen.blog.module.photo.service.PhotoService;
import com.chen.blog.common.util.BeanCopyUtils;
import com.chen.blog.common.util.PageUtils;
import com.chen.blog.common.domain.vo.*;
import com.chen.blog.common.service.FileReferenceService;
import com.chen.blog.common.strategy.upload.context.UploadStrategyContext;
import com.chen.blog.module.photo.vo.PhotoInfoVO;
import com.chen.blog.module.photo.vo.PhotoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.chen.blog.common.constant.CommonConst.FALSE;
import static com.chen.blog.common.enums.PhotoAlbumStatusEnum.PUBLIC;

/**
 * 照片服务
 *
 * @author chenfuyun
 * @date 2021/08/04
 */
@Service
public class PhotoServiceImpl extends ServiceImpl<PhotoDao, Photo> implements PhotoService {
    @Autowired
    private PhotoDao photoDao;
    @Autowired
    private PhotoAlbumService photoAlbumService;
    @Autowired
    private UploadStrategyContext uploadStrategyContext;
    @Autowired
    private FileReferenceService fileReferenceService;

    @Override
    public PageResult<PhotoBackDTO> listPhotos(ConditionVO condition) {
        // 查询照片列表
        // 注意: isDelete 必须用三参数 eq + Objects.nonNull 判断。
        // 两参数 eq(列, null) 在 MyBatis-Plus 3.4.0 会生成 "is_delete = NULL",
        // 导致永远查不到数据(照片列表页因此显示为空)。
        Page<Photo> page = new Page<>(PageUtils.getCurrent(), PageUtils.getSize());
        Page<Photo> photoPage = photoDao.selectPage(page, new LambdaQueryWrapper<Photo>()
                .eq(Objects.nonNull(condition.getAlbumId()), Photo::getAlbumId, condition.getAlbumId())
                .eq(Objects.nonNull(condition.getIsDelete()), Photo::getIsDelete, condition.getIsDelete())
                .orderByDesc(Photo::getId)
                .orderByDesc(Photo::getUpdateTime));
        List<PhotoBackDTO> photoList = BeanCopyUtils.copyList(photoPage.getRecords(), PhotoBackDTO.class);
        return new PageResult<>(photoList, (int) photoPage.getTotal());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updatePhoto(PhotoInfoVO photoInfoVO) {
        Photo photo = BeanCopyUtils.copyObject(photoInfoVO, Photo.class);
        photoDao.updateById(photo);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void savePhotos(PhotoVO photoVO) {
        List<Photo> photoList = photoVO.getPhotoUrlList().stream().map(item -> Photo.builder()
                        .albumId(photoVO.getAlbumId())
                        .photoName(IdWorker.getIdStr())
                        .photoSrc(item)
                        .build())
                .collect(Collectors.toList());
        this.saveBatch(photoList);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updatePhotosAlbum(PhotoVO photoVO) {
        List<Photo> photoList = photoVO.getPhotoIdList().stream().map(item -> Photo.builder()
                        .id(item)
                        .albumId(photoVO.getAlbumId())
                        .build())
                .collect(Collectors.toList());
        this.updateBatchById(photoList);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updatePhotoDelete(DeleteVO deleteVO) {
        // 更新照片状态
        List<Photo> photoList = deleteVO.getIdList().stream().map(item -> Photo.builder()
                        .id(item)
                        .isDelete(deleteVO.getIsDelete())
                        .build())
                .collect(Collectors.toList());
        this.updateBatchById(photoList);
        // 若恢复照片所在的相册已删除，恢复相册
        if (deleteVO.getIsDelete().equals(FALSE)) {
            List<PhotoAlbum> photoAlbumList = photoDao.selectList(new LambdaQueryWrapper<Photo>()
                            .select(Photo::getAlbumId)
                            .in(Photo::getId, deleteVO.getIdList())
                            .groupBy(Photo::getAlbumId))
                    .stream()
                    .map(item -> PhotoAlbum.builder()
                            .id(item.getAlbumId())
                            .isDelete(FALSE)
                            .build())
                    .collect(Collectors.toList());
            photoAlbumService.updateBatchById(photoAlbumList);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deletePhotos(List<Integer> photoIdList) {
        if (photoIdList == null || photoIdList.isEmpty()) {
            return;
        }

        List<Photo> photoList = photoDao.selectBatchIds(photoIdList);
        Set<String> removableUrls = photoList.stream()
                .map(Photo::getPhotoSrc)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (!removableUrls.isEmpty()) {
            Set<String> referencedUrls = fileReferenceService.findReferencedUrls(
                    removableUrls, photoIdList, Collections.emptyList(), Collections.emptyList());
            removableUrls.removeAll(referencedUrls);
            removableUrls.forEach(uploadStrategyContext::deleteFile);
        }
        photoDao.deleteBatchIds(photoIdList);
    }

    @Override
    public PhotoDTO listPhotosByAlbumId(Integer albumId) {
        // 查询相册信息
        PhotoAlbum photoAlbum = photoAlbumService.getOne(new LambdaQueryWrapper<PhotoAlbum>()
                .eq(PhotoAlbum::getId, albumId)
                .eq(PhotoAlbum::getIsDelete, FALSE)
                .eq(PhotoAlbum::getStatus, PUBLIC.getStatus()));
        if (Objects.isNull(photoAlbum)) {
            throw new BizException("相册不存在");
        }
        // 查询照片列表
        Page<Photo> page = new Page<>(PageUtils.getCurrent(), PageUtils.getSize());
        List<PhotoListDTO> photoList = photoDao.selectPage(page, new LambdaQueryWrapper<Photo>()
                        .select(Photo::getId, Photo::getPhotoName, Photo::getPhotoDesc, Photo::getPhotoSrc)
                        .eq(Photo::getAlbumId, albumId)
                        .eq(Photo::getIsDelete, FALSE)
                        .orderByDesc(Photo::getId))
                .getRecords()
                .stream()
                .map(item -> PhotoListDTO.builder()
                        .id(item.getId())
                        .photoName(item.getPhotoName())
                        .photoDesc(item.getPhotoDesc())
                        .photoSrc(item.getPhotoSrc())
                        .build())
                .collect(Collectors.toList());
        return PhotoDTO.builder()
                .photoAlbumCover(photoAlbum.getAlbumCover())
                .photoAlbumName(photoAlbum.getAlbumName())
                .photoList(photoList)
                .build();
    }

}
