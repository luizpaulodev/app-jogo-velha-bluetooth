package com.luizpaulo.adapter;

import android.bluetooth.BluetoothDevice;

public class ListaBluetooth {

	private String estado;
	private BluetoothDevice device;
	
	public ListaBluetooth(BluetoothDevice device, String estado) {
		super();
		this.device = device;
		this.estado = estado;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public BluetoothDevice getDevice() {
		return device;
	}

	public void setDevice(BluetoothDevice device) {
		this.device = device;
	}
}
