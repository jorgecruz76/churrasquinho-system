package br.com.churrasquinho.dto;

import br.com.churrasquinho.entity.Produto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class ItemCarrinhoDTO {

    private Produto produto;
    private Integer quantidade;

    public BigDecimal getSubtotal() {
        if (produto == null || produto.getPreco() == null) {
            return BigDecimal.ZERO;
        }
        return produto.getPreco()
                .multiply(BigDecimal.valueOf(quantidade));
    }
}