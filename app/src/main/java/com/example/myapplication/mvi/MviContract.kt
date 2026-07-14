package com.example.myapplication.mvi

/**
 * UI 状态接口，所有界面的状态类都应实现此接口
 */
interface IUiState

/**
 * 用户意图接口，所有界面的用户操作意图类都应实现此接口
 */
interface IUiIntent

/**
 * UI 副作用接口，用于处理一次性事件（如 Toast, 导航等）
 */
interface IUiEffect