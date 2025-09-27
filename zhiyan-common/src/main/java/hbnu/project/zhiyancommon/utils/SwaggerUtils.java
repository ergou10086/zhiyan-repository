package hbnu.project.zhiyancommon.utils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import hbnu.project.zhiyancommon.domain.R;

import java.lang.annotation.Annotation;

/**
 * Swagger工具类
 * 提供常用的Swagger注解组合和工具方法
 *
 * @author ErgouTree
 */
public class SwaggerUtils {

    /**
     * 创建标准的成功响应注解
     */
    public static ApiResponse successResponse(String description) {
        return new ApiResponse() {
            @Override
            public String responseCode() {
                return "200";
            }

            @Override
            public String description() {
                return description;
            }

            @Override
            public Content[] content() {
                return new Content[]{
                        new Content() {
                            @Override
                            public Schema schema() {
                                return new Schema() {
                                    @Override
                                    public Class implementation() {
                                        return R.class;
                                    }

                                    @Override
                                    public Class annotationType() {
                                        return Schema.class;
                                    }
                                };
                            }

                            @Override
                            public Class annotationType() {
                                return Content.class;
                            }
                        }
                };
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return ApiResponse.class;
            }
        };
    }

    /**
     * 创建分页查询参数注解
     */
    public static Parameter[] pageParameters() {
        return new Parameter[]{
                createParameter("page", "页码", "0", "query"),
                createParameter("size", "每页大小", "10", "query"),
                createParameter("sort", "排序字段", "id,desc", "query")
        };
    }

    /**
     * 创建参数注解
     */
    public static Parameter createParameter(String name, String description, String example, String in) {
        return new Parameter() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public String description() {
                return description;
            }

            @Override
            public String example() {
                return example;
            }

            @Override
            public ParameterIn in() {
                return ParameterIn.valueOf(in.toUpperCase());
            }

            @Override
            public boolean required() {
                return false;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return Parameter.class;
            }

            @Override
            public Schema schema() {
                return new Schema() {
                    @Override
                    public Class type() {
                        return String.class;
                    }

                    @Override
                    public Class annotationType() {
                        return Schema.class;
                    }
                };
            }
        };
    }

    /**
     * 创建标准的操作注解
     */
    public static class OperationBuilder {
        private String summary;
        private String description;
        private String[] tags;

        public OperationBuilder summary(String summary) {
            this.summary = summary;
            return this;
        }

        public OperationBuilder description(String description) {
            this.description = description;
            return this;
        }

        public OperationBuilder tags(String... tags) {
            this.tags = tags;
            return this;
        }

        public Operation build() {
            return new Operation() {
                @Override
                public String summary() {
                    return OperationBuilder.this.summary != null ? OperationBuilder.this.summary : "";
                }

                @Override
                public String description() {
                    return OperationBuilder.this.description != null ? OperationBuilder.this.description : "";
                }

                @Override
                public String[] tags() {
                    return OperationBuilder.this.tags != null ? OperationBuilder.this.tags : new String[0];
                }

                @Override
                public Class<? extends Annotation> annotationType() {
                    return Operation.class;
                }
            };
        }
    }

    /**
     * 创建操作构建器
     */
    public static OperationBuilder operation() {
        return new OperationBuilder();
    }
}
