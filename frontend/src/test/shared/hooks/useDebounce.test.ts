import { renderHook, act } from '@testing-library/react'
import { vi, beforeEach, afterEach } from 'vitest'
import { useDebounce } from '../../../shared/hooks/useDebounce'

beforeEach(() => {
  vi.useFakeTimers()
})

afterEach(() => {
  vi.useRealTimers()
})

describe('useDebounce', () => {
  it('returns the initial value immediately', () => {
    const { result } = renderHook(() => useDebounce('initial', 300))
    expect(result.current).toBe('initial')
  })

  it('does not update before the delay elapses', () => {
    const { result, rerender } = renderHook(({ value }) => useDebounce(value, 300), {
      initialProps: { value: 'initial' },
    })
    rerender({ value: 'updated' })
    act(() => { vi.advanceTimersByTime(299) })
    expect(result.current).toBe('initial')
  })

  it('updates after the delay elapses', () => {
    const { result, rerender } = renderHook(({ value }) => useDebounce(value, 300), {
      initialProps: { value: 'initial' },
    })
    rerender({ value: 'updated' })
    act(() => { vi.advanceTimersByTime(300) })
    expect(result.current).toBe('updated')
  })

  it('fires once for multiple rapid changes — only the last value is kept', () => {
    const { result, rerender } = renderHook(({ value }) => useDebounce(value, 300), {
      initialProps: { value: 'a' },
    })
    rerender({ value: 'b' })
    rerender({ value: 'c' })
    rerender({ value: 'd' })
    act(() => { vi.advanceTimersByTime(300) })
    expect(result.current).toBe('d')
  })
})
