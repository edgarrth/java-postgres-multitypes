package pe.axiz.paymentprocessing.domain.port.in;

import pe.axiz.paymentprocessing.domain.model.DigitalCertificate;
import pe.axiz.paymentprocessing.domain.model.DigitalCertificateMetadata;
import java.util.List;

public interface DigitalCertificateUseCase {
    DigitalCertificate register(String alias, String owner, String algorithm, byte[] pemContent);
    DigitalCertificate getByAlias(String alias);
    List<DigitalCertificateMetadata> listMetadata();
}
