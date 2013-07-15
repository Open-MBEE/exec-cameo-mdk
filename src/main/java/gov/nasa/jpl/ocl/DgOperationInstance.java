package gov.nasa.jpl.ocl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EParameter;
import org.eclipse.ocl.ecore.internal.OCLStandardLibraryImpl;

public class DgOperationInstance implements DgOperation {
	private String name;
	private String annotationName;
	private CallOperation operation;
	private List<EParameter> parameters = new ArrayList<EParameter>();

	@Override
	public void addParameter(EParameter parameter) {
		parameter.setEType(OCLStandardLibraryImpl.INSTANCE.getString());

		this.parameters.add(parameter);
	}

	@Override
	public Object callOperation(Object source, Object[] args) {
		return operation.callOperation(source, args);
	}

	@Override
	public boolean checkOperationName(String operationName) {
		if (name.equals(operationName)) {
			return true;
		}
		return false;
	}

	@Override
	public String getAnnotationName() {
		return annotationName;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<EParameter> getParameters() {
		return parameters;
	}

	@Override
	public void setAnnotationName(String annotationName) {
		this.annotationName = annotationName;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void setOperation(CallOperation operation) {
		this.operation = operation;
	}

}
