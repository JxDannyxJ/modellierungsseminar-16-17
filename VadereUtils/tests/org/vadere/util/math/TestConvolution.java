package org.vadere.util.math;

import com.nativelibs4java.opencl.*;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.vadere.util.logging.Timer;
import org.vadere.util.math.CLConvolution;
import org.vadere.util.math.Convolution;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestConvolution {

	private static Logger logger = LogManager.getLogger(TestConvolution.class);

	@Before
	public void setUp() throws Exception {}

	@Test
	public void testSingleStepConvolution() {
		float[] kernel = new float[] {
				1, 2, 3,
				4, 5, 6,
				7, 8, 9
		};

		float[] inMatrix = new float[] {
				2, 1, 1, 1,
				1, 1, 1, -1,
				1, 1, 1, 1,
				1, 1, 1, 0
		};

		assertTrue(Convolution.convolve(inMatrix, kernel, 4, 4, 3, 1, 1) == 2 + 2 + 3 + 4 + 5 + 6 + 7 + 8 + 9.0);
		assertTrue(Convolution.convolve(inMatrix, kernel, 4, 4, 3, 2, 2) == 1 + 2 - 3 + 4 + 5 + 6 + 7 + 8);
		assertTrue(Convolution.convolve(inMatrix, kernel, 4, 4, 3, 3, 3) == 4 + 1 + 2);
		assertTrue(Convolution.convolve(inMatrix, kernel, 4, 4, 3, 0, 0) == 8 + 10 + 9 + 6);
	}

	@Test
	public void testConvolution() throws IOException {
		int inputWidth = 1000;
		int inputHeight = 1000;
		int kernelWidth = 31;
		float[] kernel = Convolution.generateFloatGaussianKernel(kernelWidth, (float) Math.sqrt(0.7));
		float[] input = Convolution.generdateInputMatrix(inputWidth * inputHeight);

		float[] javaOutput = Convolution.convolve(input, kernel, inputWidth, inputHeight, kernelWidth);
		CLConvolution clConvolution = new CLConvolution();
		float[] clOutput = clConvolution.convolve(input, inputWidth, inputHeight, kernel, kernelWidth);

		equalsMatrixValues(javaOutput, clOutput, 0.00001f);
	}

	@Test
	public void testSmallConvolutionCol() throws IOException {
		int inputWidth = 6;
		int inputHeight = 1;
		int kernelWidth = 3;

		float[] rowVector = new float[] {0.5f, 0.2f, 0.3f};
		float[] input = new float[] {1f, 1f, 1f, 0f, 0f, 0f};
		float[] result = new float[] {0.5f, 1.0f, 0.7f, 0.5f, 0f, 0f};

		float[] output = Convolution.convolveCol(input, rowVector, inputWidth, inputHeight, kernelWidth);

		CLConvolution clConvolution = new CLConvolution();
		float[] clOutput = clConvolution.convolveCol(input, inputWidth, inputHeight, rowVector, kernelWidth);

		equalsMatrixValues(result, output, 0f);
		equalsMatrixValues(result, clOutput, 0f);
	}

	@Test
	public void testSmallConvolutionRow() throws IOException {
		int inputWidth = 1;
		int inputHeight = 6;
		int kernelWidth = 3;

		float[] rowVector = new float[] {0.5f, 0.2f, 0.3f};
		float[] input = new float[] {1f, 1f, 1f, 0f, 0f, 0f};
		float[] result = new float[] {0.5f, 1.0f, 0.7f, 0.5f, 0f, 0f};

		float[] output = Convolution.convolveRow(input, rowVector, inputWidth, inputHeight, kernelWidth);

		CLConvolution clConvolution = new CLConvolution();
		float[] clOutput = clConvolution.convolveRow(input, inputWidth, inputHeight, rowVector, kernelWidth);

		equalsMatrixValues(result, output, 0f);
		equalsMatrixValues(result, clOutput, 0f);
	}

	@Test
	public void testConvolutionRow() throws IOException {
		int inputWidth = 500;
		int inputHeight = 300;
		int kernelWidth = 31;
		float[] rowVector = Convolution.floatGaussian1DKernel(kernelWidth, (float) Math.sqrt(0.7));
		float[] input = Convolution.generdateInputMatrix(inputWidth * inputHeight);

		float[] output = Convolution.convolveRow(input, rowVector, inputWidth, inputHeight, kernelWidth);

		CLConvolution clConvolution = new CLConvolution();
		float[] clOutput = clConvolution.convolveRow(input, inputWidth, inputHeight, rowVector, kernelWidth);


		equalsMatrixValues(output, clOutput, 0.00001f);
	}

	@Test
	public void testConvolutionSeperate() throws IOException {
		int inputWidth = 500;
		int inputHeight = 300;
		int kernelWidth = 31;
		float[] kernel = Convolution.generateFloatGaussianKernel(kernelWidth, 0.7f);
		float[] seperateKernel = Convolution.floatGaussian1DKernel(kernelWidth, 0.7f);
		float[] input = Convolution.generdateInputMatrix(inputWidth * inputHeight);

		float[] nonSeperate = Convolution.convolve(input, kernel, inputWidth, inputHeight, kernelWidth);
		float[] seperate = Convolution.convolveSeperate(input, seperateKernel, seperateKernel, inputWidth, inputHeight,
				kernelWidth);

		CLConvolution clGPUConvolution = new CLConvolution();
		float[] clCPUOutput =
				clGPUConvolution.convolveSperate(input, inputWidth, inputHeight, seperateKernel, kernelWidth);

		equalsMatrixValues(seperate, nonSeperate, 0.00001f);
		equalsMatrixValues(clCPUOutput, nonSeperate, 0.00001f);
	}

	@Test
	public void testConvolutionPerformance() throws IOException {
		int inputWidth = 500;
		int inputHeight = 500;
		int kernelWidth = 91;
		Timer timer = new Timer();
		float[] kernel = Convolution.generateFloatGaussianKernel(kernelWidth, (float) Math.sqrt(0.7));
		float[] separateKernel = Convolution.floatGaussian1DKernel(kernelWidth, (float) Math.sqrt(0.7));
		float[] input = Convolution.generdateInputMatrix(inputWidth * inputHeight);

		CLConvolution clGPUConvolution = new CLConvolution();
		CLConvolution clCPUConvolution =
				new CLConvolution(CLPlatform.DeviceFeature.CPU, CLPlatform.DeviceFeature.MaxComputeUnits);

		// initial the .cl program
		float[] clGPUOutput = clGPUConvolution.convolve(input, inputWidth, inputHeight, kernel, kernelWidth);
		float[] clGPUSeperateOutput =
				clGPUConvolution.convolveSperate(input, inputWidth, inputHeight, separateKernel, kernelWidth);
		float[] clCPUOutput = clCPUConvolution.convolve(input, inputWidth, inputHeight, kernel, kernelWidth);

		System.out.println("Start Single Core CPU java native convolution");
		timer.start();
		float[] javaOutput = Convolution.convolve(input, kernel, inputWidth, inputHeight, kernelWidth);
		timer.end();
		System.out.println(timer);

		System.out
				.println("Start GPU separated convolution " + clGPUConvolution.getContext().getDevices()[0].getName());
		timer.start();
		clGPUSeperateOutput =
				clGPUConvolution.convolveSperate(input, inputWidth, inputHeight, separateKernel, kernelWidth);
		timer.end();
		System.out.println(timer);

		System.out.println("Start GPU convolution " + clGPUConvolution.getContext().getDevices()[0].getName());
		timer.start();
		clGPUOutput = clGPUConvolution.convolve(input, inputWidth, inputHeight, kernel, kernelWidth);
		timer.end();
		System.out.println(timer);

		System.out.println("Start CPU convolution " + clCPUConvolution.getContext().getDevices()[0].getName());
		timer.start();
		clGPUOutput = clCPUConvolution.convolve(input, inputWidth, inputHeight, kernel, kernelWidth);
		timer.end();
		System.out.println(timer);

		System.out
				.println("Start CPU separated convolution " + clCPUConvolution.getContext().getDevices()[0].getName());
		timer.start();
		clGPUOutput = clCPUConvolution.convolveSperate(input, inputWidth, inputHeight, separateKernel, kernelWidth);
		timer.end();
		System.out.println(timer);

		equalsMatrixValues(javaOutput, clCPUOutput, 0.00001f);
		equalsMatrixValues(javaOutput, clGPUSeperateOutput, 0.00001f);

	}

	private static void equalsMatrixValues(final float[] m1, final float[] m2, final float epsilon) {
		assertTrue(m1.length == m2.length);
		for (int i = 0; i < m1.length; i++) {
			assertTrue("computeGodunovDifference: " + i + ", " + m1[i] + ", " + m2[i] + ", " + Math.abs(m1[i] - m2[i]),
					Math.abs(m1[i] - m2[i]) <= epsilon);
		}
	}

	private static int countUnEqualsMatrixValues(final float[] m1, final float[] m2) {
		assertTrue(m1.length == m2.length);
		int count = 0;
		for (int i = 0; i < m1.length; i++) {
			if (m1[i] != m2[i]) {
				count++;
			}
		}

		return count;
	}


}
