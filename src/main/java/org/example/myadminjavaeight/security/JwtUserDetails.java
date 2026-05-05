package org.example.myadminjavaeight.security;

import java.util.Collection;
import java.util.Collections;

import org.example.myadminjavaeight.domain.entity.sys.SysUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * JWT 用户详情类 - Spring Security 与业务用户实体的桥梁
 *
 * 设计模式：适配器模式（Adapter Pattern）
 * 将业务层的 SysUser 实体适配为 Spring Security 的 UserDetails 接口，
 * 使得 Spring Security 能够识别和使用业务用户信息。
 *
 * 为什么需要这个类？
 * - Spring Security 要求用户信息实现 UserDetails 接口
 * - 业务实体 SysUser 通常不直接实现该接口（保持领域模型纯净）
 * - 此类充当"翻译官"，将业务字段映射为 Security 需要的格式
 *
 * 核心职责：
 * 1. 封装用户认证信息（用户名、密码）
 * 2. 提供用户状态（是否启用、是否锁定）
 * 3. 提供用户权限集合（角色/权限）
 * 4. 保存业务用户ID，供后续业务逻辑使用
 *
 * @see SysUser 业务用户实体
 * @see UserDetails Spring Security 用户详情接口
 * @see JwtAuthenticationFilter 认证过滤器，负责创建本类实例
 */
public class JwtUserDetails implements UserDetails {
    /** 序列化版本号，用于确保序列化/反序列化的兼容性 */
    private static final long serialVersionUID = 1L;

    /** 用户唯一标识ID（业务主键），用于关联其他业务数据 */
    private final Long userId;

    /** 用户名，用于身份标识和登录 */
    private final String username;

    /** 加密后的密码，Spring Security 会用它进行密码验证 */
    private final String password;

    /** 账户是否启用：true=正常，false=禁用（如管理员停用账号） */
    private final boolean enabled;

    /** 账户是否未锁定：true=正常，false=锁定（如登录失败次数过多） */
    private final boolean accountNonLocked;

    /**
     * 构造函数 - 将业务用户实体转换为 Spring Security 用户详情
     *
     * 转换逻辑：
     * - 直接映射：userId, username, password
     * - 状态转换：status=1 表示启用，其他值表示禁用
     * - 锁定判断：lockRemainingSeconds > 0 表示锁定中
     *
     * @param sysUser 业务用户实体，包含数据库中的用户信息
     */
    public JwtUserDetails(SysUser sysUser) {
        // 直接映射用户ID、用户名、密码
        this.userId = sysUser.getId();
        this.username = sysUser.getUsername();
        this.password = sysUser.getPassword();

        // 状态转换：status=1 表示账户启用，其他值表示禁用
        // 使用 null 检查避免 NullPointerException
        this.enabled = sysUser.getStatus() != null && sysUser.getStatus() == 1;

        // 锁定判断：如果剩余锁定时间 > 0，则账户被锁定
        // 例如：登录失败5次，锁定2小时，lockRemainingSeconds 会 > 0
        Long lockRemaining = sysUser.getLockRemainingSeconds();
        this.accountNonLocked = !(lockRemaining != null && lockRemaining > 0);
    }

    /**
     * 获取用户ID（自定义方法，非 UserDetails 接口）
     *
     * 为什么需要这个方法？
     * - UserDetails 接口没有提供用户ID的获取方法
     * - 业务逻辑中经常需要根据用户ID查询数据
     * - 通过此方法可以方便地获取用户ID，而不需要强转
     *
     * 使用示例：
     * // 在 Controller 中获取当前用户ID
     * JwtUserDetails userDetails = (JwtUserDetails) SecurityContextHolder.getContext()
     *         .getAuthentication().getPrincipal();
     * Long userId = userDetails.getUserId();
     *
     * @return 用户唯一标识ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 获取用户权限集合（GrantedAuthority）
     *
     * 当前实现：所有用户都拥有 "ROLE_USER" 角色
     * 这是一个简化实现，实际项目中应该：
     * - 从数据库查询用户的角色和权限
     * - 或者从 JWT Token 中解析权限（已在 JwtAuthenticationFilter 中实现）
     * - 返回真实的权限列表，如 ["ROLE_ADMIN", "USER_READ", "USER_WRITE"]
     *
     * 权限命名规范：
     * - 角色以 "ROLE_" 前缀开头，如 "ROLE_ADMIN", "ROLE_USER"
     * - 权限直接使用操作名称，如 "USER_READ", "USER_WRITE"
     * - Spring Security 的 hasRole("ADMIN") 会自动添加 "ROLE_" 前缀
     *
     * @return 用户权限集合，包含至少一个默认角色
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 简化实现：所有用户默认拥有 ROLE_USER 角色
        // 实际项目中应从数据库或 JWT Token 中获取真实权限
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    /**
     * 获取用户密码（加密后）
     *
     * Spring Security 会使用此密码进行身份验证：
     * - 登录时：与用户输入的密码进行比对
     * - 密码编码：通常使用 BCrypt 等强哈希算法
     * - 安全考虑：密码永远不会以明文形式存储或传输
     *
     * @return 加密后的密码字符串
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * 获取用户名
     *
     * Spring Security 使用用户名进行：
     * - 身份标识：在日志、审计中记录用户操作
     * - 登录验证：与密码组合进行身份认证
     * - 用户查找：通过 UserDetailsService 加载用户信息
     *
     * 注意事项：
     * - 用户名应该是唯一的
     * - 通常不区分大小写（取决于业务需求）
     * - 可以是邮箱、手机号或自定义用户名
     *
     * @return 用户名字符串
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * 账户是否未过期
     *
     * <p>当前实现：始终返回 true（账户永不过期）</p>
     * <p>实际项目中，可以：</p>
     * <ul>
     *   <li>检查账户有效期：如企业员工账户在离职后过期</li>
     *   <li>检查试用期：如试用账户30天后过期</li>
     *   <li>检查会员等级：如VIP会员到期后降级</li>
     * </ul>
     *
     * <p>扩展建议：</p>
     * <pre>{@code
     * // 在 SysUser 中添加过期时间字段
     * private LocalDateTime accountExpireTime;
     *
     * // 在构造函数中判断
     * this.accountNonExpired = sysUser.getAccountExpireTime() == null
     *         || sysUser.getAccountExpireTime().isAfter(LocalDateTime.now());
     * }</pre>
     *
     * @return true 表示账户未过期，false 表示账户已过期
     */
    @Override
    public boolean isAccountNonExpired() {
        // 简化实现：账户永不过期
        // 实际项目中应根据业务需求判断账户有效期
        return true;
    }

    /**
     * 账户是否未锁定
     *
     * <p>锁定机制：当用户登录失败次数过多时，系统会临时锁定账户</p>
     * <p>当前实现：根据 {@code lockRemainingSeconds} 判断</p>
     * <ul>
     *   <li>lockRemainingSeconds > 0：账户被锁定，返回 false</li>
     *   <li>lockRemainingSeconds <= 0 或 null：账户正常，返回 true</li>
     * </ul>
     *
     * <p>安全策略：</p>
     * <ul>
     *   <li>防止暴力破解：连续登录失败5次，锁定2小时</li>
     *   <li>自动解锁：锁定时间结束后自动恢复</li>
     *   <li>管理员解锁：管理员可以手动解锁账户</li>
     * </ul>
     *
     * @return true 表示账户未锁定，false 表示账户被锁定
     */
    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    /**
     * 凭证（密码）是否未过期
     *
     * <p>当前实现：始终返回 true（密码永不过期）</p>
     * <p>实际项目中，可以：</p>
     * <ul>
     *   <li>强制密码更新：如每90天要求修改密码</li>
     *   <li>密码策略：如密码过期后必须修改才能登录</li>
     *   <li>安全合规：某些行业要求定期更换密码</li>
     * </ul>
     *
     * <p>扩展建议：</p>
     * <pre>{@code
     * // 在 SysUser 中添加密码过期时间字段
     * private LocalDateTime passwordExpireTime;
     *
     * // 在构造函数中判断
     * this.credentialsNonExpired = sysUser.getPasswordExpireTime() == null
     *         || sysUser.getPasswordExpireTime().isAfter(LocalDateTime.now());
     * }</pre>
     *
     * @return true 表示凭证未过期，false 表示凭证已过期
     */
    @Override
    public boolean isCredentialsNonExpired() {
        // 简化实现：密码永不过期
        // 实际项目中应根据安全策略判断密码有效期
        return true;
    }

    /**
     * 账户是否启用
     *
     * <p>当前实现：根据 {@code status} 字段判断</p>
     * <ul>
     *   <li>status = 1：账户启用，返回 true</li>
     *   <li>status != 1：账户禁用，返回 false</li>
     * </ul>
     *
     * <p>使用场景：</p>
     * <ul>
     *   <li>管理员停用：管理员手动禁用违规账户</li>
     *   <li>系统维护：系统升级时临时禁用所有账户</li>
     *   <li>合规要求：如员工离职后禁用账户</li>
     * </ul>
     *
     * <p>与锁定的区别：</p>
     * <ul>
     *   <li>禁用（Disabled）：长期状态，需要管理员手动启用</li>
     *   <li>锁定（Locked）：临时状态，到期自动解锁</li>
     * </ul>
     *
     * @return true 表示账户启用，false 表示账户禁用
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

}
