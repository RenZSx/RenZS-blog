package com.chen.blog.module.photo.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chen.blog.common.domain.vo.ConditionVO;
import com.chen.blog.module.photo.dto.PhotoAlbumBackDTO;
import com.chen.blog.module.photo.dto.PhotoAlbumDTO;
import com.chen.blog.module.photo.entity.PhotoAlbum;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 相册映射器
 *
 * @author chenfuyun
 * @date 2021/08/04
 */
@Repository
public interface PhotoAlbumDao extends BaseMapper<PhotoAlbum> {

    /**
     * 查询后台相册列表
     *
     * @param current   页码
     * @param size      大小
     * @param condition 条件
     * @return {@link List < PhotoAlbumBackDTO >} 相册列表
     */
    List<PhotoAlbumBackDTO> listPhotoAlbumBacks(@Param("current") Long current, @Param("size") Long size, @Param("condition") ConditionVO condition);

    /**
     * 查询前台公开相册列表，并统计每个相册下的照片数量。
     *
     * @return {@link List<PhotoAlbumDTO>} 公开相册列表
     */
    List<PhotoAlbumDTO> listPhotoAlbums();

}




