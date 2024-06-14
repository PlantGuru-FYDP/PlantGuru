#include <vector>
#include <functional>

class SchedulingBlock {
public:
  std::function<void()> task;
  uint32_t lastRun;
  uint32_t interval;

  SchedulingBlock(std::function<void()> task, uint32_t interval)
    : task(task), interval(interval) {
    lastRun = 0;
  }

  void run() {
    if (millis() - lastRun >= interval) {
      task();
      lastRun = millis();
    }
  }
};

class Scheduler {
public:
  std::vector<SchedulingBlock> blocks;

  void add(std::function<void()> task, uint32_t interval) {
    blocks.push_back(SchedulingBlock(task, interval));
  }

  void run() {
    for (SchedulingBlock& block : blocks) {
      block.run();
    }
  }
};