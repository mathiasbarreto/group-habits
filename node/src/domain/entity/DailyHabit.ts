import { Habit } from "./Habit"

export class DailyHabit{
  public date!: Date
  public checked: boolean = false
  public habit!: Habit

  constructor(date: Date) {
    this.date = date
  }

  toggleCheck() {
    this.checked = !this.checked
  }
}
