package uoc.edu.raulsuarez.devsecop.service;

import org.springframework.stereotype.Service;
import uoc.edu.raulsuarez.devsecop.dto.Calcul;

import java.util.Arrays;
import java.util.List;

/**
 * {@inheritDoc}
 */
@Service
public class CalculatorServiceImpl implements CalculatorService {

    /**
     * {@inhericDoc}
     */
    @Override
    public Integer addition(Integer x, Integer... yza) {
        return Arrays.stream(yza).reduce(x, (op1, op2) -> op1+op2);
    }

    /**
     * {@inhericDoc}
     */
    @Override
    public Integer subtraction(Integer x, Integer... yza) {
        return Arrays.stream(yza).reduce(x, (op1, op2) -> op1-op2);
    }

    /**
     * {@inhericDoc}
     */
    @Override
    public Integer multiplication(Integer x, Integer... yza) {
        return Arrays.stream(yza).reduce(x, (op1, op2) -> op1*op2);
    }

    /**
     * {@inhericDoc}
     */
    @Override
    public Integer division(Integer x, Integer... yza) {
        return Arrays.stream(yza).reduce(x, (op1, op2) -> op1/op2);
    }

    @Override
    public Integer calculate(Calcul calcul) throws Exception {
        List<Integer> xyza = calcul.getItems();
        Integer head = xyza.get(0);
        List<Integer> tail = xyza.subList(1, xyza.size());
        Integer[] xya = new Integer[tail.size()];
        xya = tail.toArray(xya);

        Integer result;

        switch (calcul.getOperation()) {
            case "addition":
                result = this.addition(head, xya);
                break;
            case "subtraction":
                result = this.subtraction(head, xya);
                break;
            case "multiplication":
                result = this.multiplication(head, xya);
                break;
            case "division":
                result = this.division(head, xya);
                break;
            default:
                throw new Exception("Undefined operation");
        }
        return result;
    }
}
