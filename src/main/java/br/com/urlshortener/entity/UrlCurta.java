package br.com.urlshortener.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.time.LocalDateTime;

/**
 * Representa uma URL encurtada armazenada no banco.
 * Quando o usuario informa um alias, ele e gravado direto no campo "codigo".
 */
@Entity
@Table(name = "url_curta", uniqueConstraints = @UniqueConstraint(columnNames = "codigo"))
public class UrlCurta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(name = "url_original", nullable = false, length = 2048)
    private String urlOriginal;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    public UrlCurta() {
        // exigido pelo JPA
    }

    public UrlCurta(String codigo, String urlOriginal) {
        this.codigo = codigo;
        this.urlOriginal = urlOriginal;
        this.criadoEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getUrlOriginal() {
        return urlOriginal;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}
