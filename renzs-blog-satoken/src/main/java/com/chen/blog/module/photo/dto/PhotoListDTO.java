package com.chen.blog.module.photo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 前台相册照片信息。
 *
 * @author ChenFY
 * @date 2026/05/15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PhotoListDTO {

    /**
     * 照片id。
     */
    private Integer id;

    /**
     * 照片名称。
     */
    private String photoName;

    /**
     * 照片描述。
     */
    private String photoDesc;

    /**
     * 照片地址。
     */
    private String photoSrc;

}
